import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, Index } from 'typeorm';

@Entity('audit_log')
@Index('idx_audit_actor_date', ['actorId', 'createdAt'])
@Index('idx_audit_scope', ['region', 'idEtablissement', 'createdAt'])
export class AuditLog {
  @PrimaryGeneratedColumn('uuid', { name: 'id_audit' }) idAudit: string;
  @Column({ name: 'actor_id', type: 'char', length: 36, nullable: true }) actorId: string | null;
  @Column({ name: 'actor_role', type: 'varchar', length: 30, nullable: true }) actorRole: string | null;
  @Column({ type: 'varchar', length: 80 }) action: string;
  @Column({ name: 'entity_type', type: 'varchar', length: 80 }) entityType: string;
  @Column({ name: 'entity_id', type: 'char', length: 36, nullable: true }) entityId: string | null;
  @Column({ type: 'varchar', length: 120, nullable: true }) region: string | null;
  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true }) idEtablissement: string | null;
  @Column({ name: 'old_value', type: 'json', nullable: true }) oldValue: any;
  @Column({ name: 'new_value', type: 'json', nullable: true }) newValue: any;
  @Column({ name: 'ip_address', type: 'varchar', length: 64, nullable: true }) ipAddress: string | null;
  @CreateDateColumn({ name: 'created_at', type: 'datetime' }) createdAt: Date;
}
